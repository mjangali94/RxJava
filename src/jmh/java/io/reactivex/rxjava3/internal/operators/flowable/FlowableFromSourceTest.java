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

import java.util.List;
import org.junit.*;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Cancellable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFromSourceTest extends RxJavaTest {

    PublishAsyncEmitter source;

    PublishAsyncEmitterNoCancel sourceNoCancel;

    TestSubscriberEx<Integer> ts;

    @Before
    public void before() {
        source = new PublishAsyncEmitter();
        sourceNoCancel = new PublishAsyncEmitterNoCancel();
        ts = new TestSubscriberEx<>(0L);
    }

    @Test
    public void normalBuffered() {
        Flowable.create(source, BackpressureStrategy.BUFFER).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.request(1);
        ts.assertValue(1);
        Assert.assertEquals(0, source.requested());
        ts.request(1);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalDrop() {
        Flowable.create(source, BackpressureStrategy.DROP).subscribe(ts);
        source.onNext(1);
        ts.request(1);
        ts.assertNoValues();
        source.onNext(2);
        source.onComplete();
        ts.assertValues(2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalLatest() {
        Flowable.create(source, BackpressureStrategy.LATEST).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.assertNoValues();
        ts.request(1);
        ts.assertValues(2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalMissing() {
        Flowable.create(source, BackpressureStrategy.MISSING).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalMissingRequested() {
        Flowable.create(source, BackpressureStrategy.MISSING).subscribe(ts);
        ts.request(2);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalError() {
        Flowable.create(source, BackpressureStrategy.ERROR).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.assertNoValues();
        ts.assertError(MissingBackpressureException.class);
        ts.assertNotComplete();
        Assert.assertEquals("create: could not emit value due to lack of requests", ts.errors().get(0).getMessage());
    }

    @Test
    public void errorBuffered() {
        Flowable.create(source, BackpressureStrategy.BUFFER).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.request(1);
        ts.assertValue(1);
        ts.request(1);
        ts.assertValues(1, 2);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void errorLatest() {
        Flowable.create(source, BackpressureStrategy.LATEST).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.assertNoValues();
        ts.request(1);
        ts.assertValues(2);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void errorMissing() {
        Flowable.create(source, BackpressureStrategy.MISSING).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.request(1);
        ts.assertValues(1, 2);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void unsubscribedBuffer() {
        Flowable.create(source, BackpressureStrategy.BUFFER).subscribe(ts);
        ts.cancel();
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.request(1);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void unsubscribedLatest() {
        Flowable.create(source, BackpressureStrategy.LATEST).subscribe(ts);
        ts.cancel();
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.request(1);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void unsubscribedError() {
        Flowable.create(source, BackpressureStrategy.ERROR).subscribe(ts);
        ts.cancel();
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.request(1);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void unsubscribedDrop() {
        Flowable.create(source, BackpressureStrategy.DROP).subscribe(ts);
        ts.cancel();
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.request(1);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void unsubscribedMissing() {
        Flowable.create(source, BackpressureStrategy.MISSING).subscribe(ts);
        ts.cancel();
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.request(1);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void unsubscribedNoCancelBuffer() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.create(sourceNoCancel, BackpressureStrategy.BUFFER).subscribe(ts);
            ts.cancel();
            sourceNoCancel.onNext(1);
            sourceNoCancel.onNext(2);
            sourceNoCancel.onError(new TestException());
            ts.request(1);
            ts.assertNoValues();
            ts.assertNoErrors();
            ts.assertNotComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void unsubscribedNoCancelLatest() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.create(sourceNoCancel, BackpressureStrategy.LATEST).subscribe(ts);
            ts.cancel();
            sourceNoCancel.onNext(1);
            sourceNoCancel.onNext(2);
            sourceNoCancel.onError(new TestException());
            ts.request(1);
            ts.assertNoValues();
            ts.assertNoErrors();
            ts.assertNotComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void unsubscribedNoCancelError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.create(sourceNoCancel, BackpressureStrategy.ERROR).subscribe(ts);
            ts.cancel();
            sourceNoCancel.onNext(1);
            sourceNoCancel.onNext(2);
            sourceNoCancel.onError(new TestException());
            ts.request(1);
            ts.assertNoValues();
            ts.assertNoErrors();
            ts.assertNotComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void unsubscribedNoCancelDrop() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.create(sourceNoCancel, BackpressureStrategy.DROP).subscribe(ts);
            ts.cancel();
            sourceNoCancel.onNext(1);
            sourceNoCancel.onNext(2);
            sourceNoCancel.onError(new TestException());
            ts.request(1);
            ts.assertNoValues();
            ts.assertNoErrors();
            ts.assertNotComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void unsubscribedNoCancelMissing() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.create(sourceNoCancel, BackpressureStrategy.MISSING).subscribe(ts);
            ts.cancel();
            sourceNoCancel.onNext(1);
            sourceNoCancel.onNext(2);
            sourceNoCancel.onError(new TestException());
            ts.request(1);
            ts.assertNoValues();
            ts.assertNoErrors();
            ts.assertNotComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void deferredRequest() {
        Flowable.create(source, BackpressureStrategy.BUFFER).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.request(2);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void take() {
        Flowable.create(source, BackpressureStrategy.BUFFER).take(2).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.request(2);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void takeOne() {
        Flowable.create(source, BackpressureStrategy.BUFFER).take(1).subscribe(ts);
        ts.request(2);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void requestExact() {
        Flowable.create(source, BackpressureStrategy.BUFFER).subscribe(ts);
        ts.request(2);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void takeNoCancel() {
        Flowable.create(sourceNoCancel, BackpressureStrategy.BUFFER).take(2).subscribe(ts);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onNext(2);
        sourceNoCancel.onComplete();
        ts.request(2);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void takeOneNoCancel() {
        Flowable.create(sourceNoCancel, BackpressureStrategy.BUFFER).take(1).subscribe(ts);
        ts.request(2);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onNext(2);
        sourceNoCancel.onComplete();
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void unsubscribeNoCancel() {
        Flowable.create(sourceNoCancel, BackpressureStrategy.BUFFER).subscribe(ts);
        ts.request(2);
        sourceNoCancel.onNext(1);
        ts.cancel();
        sourceNoCancel.onNext(2);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void unsubscribeInline() {
        TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
            }
        };
        Flowable.create(sourceNoCancel, BackpressureStrategy.BUFFER).subscribe(ts1);
        sourceNoCancel.onNext(1);
        ts1.assertValues(1);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
    }

    @Test
    public void completeInline() {
        Flowable.create(sourceNoCancel, BackpressureStrategy.BUFFER).subscribe(ts);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onComplete();
        ts.request(2);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void errorInline() {
        Flowable.create(sourceNoCancel, BackpressureStrategy.BUFFER).subscribe(ts);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onError(new TestException());
        ts.request(2);
        ts.assertValues(1);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void requestInline() {
        TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                request(1);
            }
        };
        Flowable.create(sourceNoCancel, BackpressureStrategy.BUFFER).subscribe(ts1);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onNext(2);
        ts1.assertValues(1, 2);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
    }

    @Test
    public void unsubscribeInlineLatest() {
        TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
            }
        };
        Flowable.create(sourceNoCancel, BackpressureStrategy.LATEST).subscribe(ts1);
        sourceNoCancel.onNext(1);
        ts1.assertValues(1);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
    }

    @Test
    public void unsubscribeInlineExactLatest() {
        TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
            }
        };
        Flowable.create(sourceNoCancel, BackpressureStrategy.LATEST).subscribe(ts1);
        sourceNoCancel.onNext(1);
        ts1.assertValues(1);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
    }

    @Test
    public void completeInlineLatest() {
        Flowable.create(sourceNoCancel, BackpressureStrategy.LATEST).subscribe(ts);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onComplete();
        ts.request(2);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void completeInlineExactLatest() {
        Flowable.create(sourceNoCancel, BackpressureStrategy.LATEST).subscribe(ts);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onComplete();
        ts.request(1);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void errorInlineLatest() {
        Flowable.create(sourceNoCancel, BackpressureStrategy.LATEST).subscribe(ts);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onError(new TestException());
        ts.request(2);
        ts.assertValues(1);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void requestInlineLatest() {
        TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                request(1);
            }
        };
        Flowable.create(sourceNoCancel, BackpressureStrategy.LATEST).subscribe(ts1);
        sourceNoCancel.onNext(1);
        sourceNoCancel.onNext(2);
        ts1.assertValues(1, 2);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
    }

    static final class PublishAsyncEmitter implements FlowableOnSubscribe<Integer>, FlowableSubscriber<Integer> {

        final PublishProcessor<Integer> processor;

        FlowableEmitter<Integer> current;

        PublishAsyncEmitter() {
            this.processor = PublishProcessor.create();
        }

        long requested() {
            return current.requested();
        }

        @Override
        public void subscribe(final FlowableEmitter<Integer> t) {
            this.current = t;
            final ResourceSubscriber<Integer> as = new ResourceSubscriber<Integer>() {

                @Override
                public void onComplete() {
                    t.onComplete();
                }

                @Override
                public void onError(Throwable e) {
                    t.onError(e);
                }

                @Override
                public void onNext(Integer v) {
                    t.onNext(v);
                }
            };
            processor.subscribe(as);
            t.setCancellable(new Cancellable() {

                @Override
                public void cancel() throws Exception {
                    as.dispose();
                }
            });
            ;
        }

        @Override
        public void onSubscribe(Subscription s) {
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(Integer t) {
            processor.onNext(t);
        }

        @Override
        public void onError(Throwable e) {
            processor.onError(e);
        }

        @Override
        public void onComplete() {
            processor.onComplete();
        }
    }

    static final class PublishAsyncEmitterNoCancel implements FlowableOnSubscribe<Integer>, FlowableSubscriber<Integer> {

        final PublishProcessor<Integer> processor;

        PublishAsyncEmitterNoCancel() {
            this.processor = PublishProcessor.create();
        }

        @Override
        public void subscribe(final FlowableEmitter<Integer> t) {
            processor.subscribe(new FlowableSubscriber<Integer>() {

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onComplete() {
                    t.onComplete();
                }

                @Override
                public void onError(Throwable e) {
                    t.onError(e);
                }

                @Override
                public void onNext(Integer v) {
                    t.onNext(v);
                }
            });
        }

        @Override
        public void onSubscribe(Subscription s) {
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(Integer t) {
            processor.onNext(t);
        }

        @Override
        public void onError(Throwable e) {
            processor.onError(e);
        }

        @Override
        public void onComplete() {
            processor.onComplete();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFromSourceTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBuffered() throws java.lang.Throwable {
            this.payloads.normalBuffered.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDrop() throws java.lang.Throwable {
            this.payloads.normalDrop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalLatest() throws java.lang.Throwable {
            this.payloads.normalLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMissing() throws java.lang.Throwable {
            this.payloads.normalMissing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMissingRequested() throws java.lang.Throwable {
            this.payloads.normalMissingRequested.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalError() throws java.lang.Throwable {
            this.payloads.normalError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorBuffered() throws java.lang.Throwable {
            this.payloads.errorBuffered.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorLatest() throws java.lang.Throwable {
            this.payloads.errorLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorMissing() throws java.lang.Throwable {
            this.payloads.errorMissing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedBuffer() throws java.lang.Throwable {
            this.payloads.unsubscribedBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedLatest() throws java.lang.Throwable {
            this.payloads.unsubscribedLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedError() throws java.lang.Throwable {
            this.payloads.unsubscribedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedDrop() throws java.lang.Throwable {
            this.payloads.unsubscribedDrop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedMissing() throws java.lang.Throwable {
            this.payloads.unsubscribedMissing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedNoCancelBuffer() throws java.lang.Throwable {
            this.payloads.unsubscribedNoCancelBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedNoCancelLatest() throws java.lang.Throwable {
            this.payloads.unsubscribedNoCancelLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedNoCancelError() throws java.lang.Throwable {
            this.payloads.unsubscribedNoCancelError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedNoCancelDrop() throws java.lang.Throwable {
            this.payloads.unsubscribedNoCancelDrop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribedNoCancelMissing() throws java.lang.Throwable {
            this.payloads.unsubscribedNoCancelMissing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferredRequest() throws java.lang.Throwable {
            this.payloads.deferredRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeOne() throws java.lang.Throwable {
            this.payloads.takeOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestExact() throws java.lang.Throwable {
            this.payloads.requestExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeNoCancel() throws java.lang.Throwable {
            this.payloads.takeNoCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeOneNoCancel() throws java.lang.Throwable {
            this.payloads.takeOneNoCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeNoCancel() throws java.lang.Throwable {
            this.payloads.unsubscribeNoCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeInline() throws java.lang.Throwable {
            this.payloads.unsubscribeInline.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeInline() throws java.lang.Throwable {
            this.payloads.completeInline.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorInline() throws java.lang.Throwable {
            this.payloads.errorInline.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestInline() throws java.lang.Throwable {
            this.payloads.requestInline.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeInlineLatest() throws java.lang.Throwable {
            this.payloads.unsubscribeInlineLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeInlineExactLatest() throws java.lang.Throwable {
            this.payloads.unsubscribeInlineExactLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeInlineLatest() throws java.lang.Throwable {
            this.payloads.completeInlineLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeInlineExactLatest() throws java.lang.Throwable {
            this.payloads.completeInlineExactLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorInlineLatest() throws java.lang.Throwable {
            this.payloads.errorInlineLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestInlineLatest() throws java.lang.Throwable {
            this.payloads.requestInlineLatest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromSourceTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromSourceTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromSourceTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromSourceTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFromSourceTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromSourceTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFromSourceTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFromSourceTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normalBuffered;

            public org.junit.runners.model.Statement normalDrop;

            public org.junit.runners.model.Statement normalLatest;

            public org.junit.runners.model.Statement normalMissing;

            public org.junit.runners.model.Statement normalMissingRequested;

            public org.junit.runners.model.Statement normalError;

            public org.junit.runners.model.Statement errorBuffered;

            public org.junit.runners.model.Statement errorLatest;

            public org.junit.runners.model.Statement errorMissing;

            public org.junit.runners.model.Statement unsubscribedBuffer;

            public org.junit.runners.model.Statement unsubscribedLatest;

            public org.junit.runners.model.Statement unsubscribedError;

            public org.junit.runners.model.Statement unsubscribedDrop;

            public org.junit.runners.model.Statement unsubscribedMissing;

            public org.junit.runners.model.Statement unsubscribedNoCancelBuffer;

            public org.junit.runners.model.Statement unsubscribedNoCancelLatest;

            public org.junit.runners.model.Statement unsubscribedNoCancelError;

            public org.junit.runners.model.Statement unsubscribedNoCancelDrop;

            public org.junit.runners.model.Statement unsubscribedNoCancelMissing;

            public org.junit.runners.model.Statement deferredRequest;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement takeOne;

            public org.junit.runners.model.Statement requestExact;

            public org.junit.runners.model.Statement takeNoCancel;

            public org.junit.runners.model.Statement takeOneNoCancel;

            public org.junit.runners.model.Statement unsubscribeNoCancel;

            public org.junit.runners.model.Statement unsubscribeInline;

            public org.junit.runners.model.Statement completeInline;

            public org.junit.runners.model.Statement errorInline;

            public org.junit.runners.model.Statement requestInline;

            public org.junit.runners.model.Statement unsubscribeInlineLatest;

            public org.junit.runners.model.Statement unsubscribeInlineExactLatest;

            public org.junit.runners.model.Statement completeInlineLatest;

            public org.junit.runners.model.Statement completeInlineExactLatest;

            public org.junit.runners.model.Statement errorInlineLatest;

            public org.junit.runners.model.Statement requestInlineLatest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalBuffered = _ClassStatement.forPayload(FlowableFromSourceTest::normalBuffered, "normalBuffered", this);
            this.payloads.normalDrop = _ClassStatement.forPayload(FlowableFromSourceTest::normalDrop, "normalDrop", this);
            this.payloads.normalLatest = _ClassStatement.forPayload(FlowableFromSourceTest::normalLatest, "normalLatest", this);
            this.payloads.normalMissing = _ClassStatement.forPayload(FlowableFromSourceTest::normalMissing, "normalMissing", this);
            this.payloads.normalMissingRequested = _ClassStatement.forPayload(FlowableFromSourceTest::normalMissingRequested, "normalMissingRequested", this);
            this.payloads.normalError = _ClassStatement.forPayload(FlowableFromSourceTest::normalError, "normalError", this);
            this.payloads.errorBuffered = _ClassStatement.forPayload(FlowableFromSourceTest::errorBuffered, "errorBuffered", this);
            this.payloads.errorLatest = _ClassStatement.forPayload(FlowableFromSourceTest::errorLatest, "errorLatest", this);
            this.payloads.errorMissing = _ClassStatement.forPayload(FlowableFromSourceTest::errorMissing, "errorMissing", this);
            this.payloads.unsubscribedBuffer = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedBuffer, "unsubscribedBuffer", this);
            this.payloads.unsubscribedLatest = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedLatest, "unsubscribedLatest", this);
            this.payloads.unsubscribedError = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedError, "unsubscribedError", this);
            this.payloads.unsubscribedDrop = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedDrop, "unsubscribedDrop", this);
            this.payloads.unsubscribedMissing = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedMissing, "unsubscribedMissing", this);
            this.payloads.unsubscribedNoCancelBuffer = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedNoCancelBuffer, "unsubscribedNoCancelBuffer", this);
            this.payloads.unsubscribedNoCancelLatest = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedNoCancelLatest, "unsubscribedNoCancelLatest", this);
            this.payloads.unsubscribedNoCancelError = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedNoCancelError, "unsubscribedNoCancelError", this);
            this.payloads.unsubscribedNoCancelDrop = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedNoCancelDrop, "unsubscribedNoCancelDrop", this);
            this.payloads.unsubscribedNoCancelMissing = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribedNoCancelMissing, "unsubscribedNoCancelMissing", this);
            this.payloads.deferredRequest = _ClassStatement.forPayload(FlowableFromSourceTest::deferredRequest, "deferredRequest", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableFromSourceTest::take, "take", this);
            this.payloads.takeOne = _ClassStatement.forPayload(FlowableFromSourceTest::takeOne, "takeOne", this);
            this.payloads.requestExact = _ClassStatement.forPayload(FlowableFromSourceTest::requestExact, "requestExact", this);
            this.payloads.takeNoCancel = _ClassStatement.forPayload(FlowableFromSourceTest::takeNoCancel, "takeNoCancel", this);
            this.payloads.takeOneNoCancel = _ClassStatement.forPayload(FlowableFromSourceTest::takeOneNoCancel, "takeOneNoCancel", this);
            this.payloads.unsubscribeNoCancel = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribeNoCancel, "unsubscribeNoCancel", this);
            this.payloads.unsubscribeInline = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribeInline, "unsubscribeInline", this);
            this.payloads.completeInline = _ClassStatement.forPayload(FlowableFromSourceTest::completeInline, "completeInline", this);
            this.payloads.errorInline = _ClassStatement.forPayload(FlowableFromSourceTest::errorInline, "errorInline", this);
            this.payloads.requestInline = _ClassStatement.forPayload(FlowableFromSourceTest::requestInline, "requestInline", this);
            this.payloads.unsubscribeInlineLatest = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribeInlineLatest, "unsubscribeInlineLatest", this);
            this.payloads.unsubscribeInlineExactLatest = _ClassStatement.forPayload(FlowableFromSourceTest::unsubscribeInlineExactLatest, "unsubscribeInlineExactLatest", this);
            this.payloads.completeInlineLatest = _ClassStatement.forPayload(FlowableFromSourceTest::completeInlineLatest, "completeInlineLatest", this);
            this.payloads.completeInlineExactLatest = _ClassStatement.forPayload(FlowableFromSourceTest::completeInlineExactLatest, "completeInlineExactLatest", this);
            this.payloads.errorInlineLatest = _ClassStatement.forPayload(FlowableFromSourceTest::errorInlineLatest, "errorInlineLatest", this);
            this.payloads.requestInlineLatest = _ClassStatement.forPayload(FlowableFromSourceTest::requestInlineLatest, "requestInlineLatest", this);
        }
    }
}
