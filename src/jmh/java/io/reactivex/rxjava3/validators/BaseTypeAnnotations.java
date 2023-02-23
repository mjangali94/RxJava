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
package io.reactivex.rxjava3.validators;

import static org.junit.Assert.fail;
import java.lang.reflect.*;
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.annotations.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.DisposableContainer;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subjects.*;

/**
 * Verifies several properties.
 * <ul>
 * <li>Certain public base type methods have the {@link CheckReturnValue} present</li>
 * <li>All public base type methods have the {@link SchedulerSupport} present</li>
 * <li>All public base type methods which return Flowable have the {@link BackpressureSupport} present</li>
 * <li>All public base types that don't return Flowable don't have the {@link BackpressureSupport} present (these are copy-paste errors)</li>
 * </ul>
 */
public class BaseTypeAnnotations {

    static void checkCheckReturnValueSupport(Class<?> clazz) {
        StringBuilder b = new StringBuilder();
        for (Method m : clazz.getMethods()) {
            if (m.getDeclaringClass() == clazz) {
                boolean isSubscribeMethod = "subscribe".equals(m.getName()) && (m.getParameterTypes().length == 0 || m.getParameterTypes()[m.getParameterCount() - 1] == DisposableContainer.class);
                boolean isConnectMethod = "connect".equals(m.getName()) && m.getParameterTypes().length == 0;
                boolean isAnnotationPresent = m.isAnnotationPresent(CheckReturnValue.class);
                if (isSubscribeMethod || isConnectMethod) {
                    if (isAnnotationPresent) {
                        b.append(m.getName()).append(" method has @CheckReturnValue: ").append(m).append("\r\n");
                    }
                    continue;
                }
                if (Modifier.isPrivate(m.getModifiers()) && isAnnotationPresent) {
                    b.append("Private method has @CheckReturnValue: ").append(m).append("\r\n");
                    continue;
                }
                if (m.getReturnType().equals(Void.TYPE)) {
                    if (isAnnotationPresent) {
                        b.append("Void method has @CheckReturnValue: ").append(m).append("\r\n");
                    }
                    continue;
                }
                if (!isAnnotationPresent) {
                    b.append("Missing @CheckReturnValue: ").append(m).append("\r\n");
                }
            }
        }
        if (b.length() != 0) {
            // System.out.println(clazz);
            // System.out.println("------------------------");
            // System.out.println(b);
            fail(b.toString());
        }
    }

    static void checkSchedulerSupport(Class<?> clazz) {
        StringBuilder b = new StringBuilder();
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals("bufferSize") || m.getName().equals("parallelism")) {
                continue;
            }
            if (m.getDeclaringClass() == clazz) {
                if (!m.isAnnotationPresent(SchedulerSupport.class)) {
                    b.append("Missing @SchedulerSupport: ").append(m).append("\r\n");
                } else {
                    SchedulerSupport ann = m.getAnnotation(SchedulerSupport.class);
                    if (ann.value().equals(SchedulerSupport.CUSTOM)) {
                        boolean found = false;
                        for (Class<?> paramclazz : m.getParameterTypes()) {
                            if (Scheduler.class.isAssignableFrom(paramclazz)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            b.append("Marked with CUSTOM scheduler but no Scheduler parameter: ").append(m).append("\r\n");
                        }
                    } else {
                        for (Class<?> paramclazz : m.getParameterTypes()) {
                            if (Scheduler.class.isAssignableFrom(paramclazz)) {
                                if (!m.getName().equals("timestamp") && !m.getName().equals("timeInterval")) {
                                    b.append("Marked with specific scheduler but Scheduler parameter found: ").append(m).append("\r\n");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (b.length() != 0) {
            // System.out.println(clazz);
            // System.out.println("------------------------");
            // System.out.println(b);
            fail(b.toString());
        }
    }

    static void checkBackpressureSupport(Class<?> clazz) {
        StringBuilder b = new StringBuilder();
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals("bufferSize") || m.getName().equals("parallelism")) {
                continue;
            }
            if (m.getDeclaringClass() == clazz) {
                if (clazz == Flowable.class || clazz == ParallelFlowable.class) {
                    if (!m.isAnnotationPresent(BackpressureSupport.class)) {
                        b.append("No @BackpressureSupport annotation (being ").append(clazz.getSimpleName()).append("): ").append(m).append("\r\n");
                    }
                } else {
                    if (m.getReturnType() == Flowable.class || m.getReturnType() == ParallelFlowable.class) {
                        if (!m.isAnnotationPresent(BackpressureSupport.class)) {
                            b.append("No @BackpressureSupport annotation (having ").append(m.getReturnType().getSimpleName()).append(" return): ").append(m).append("\r\n");
                        }
                    } else {
                        boolean found = false;
                        for (Class<?> paramclazz : m.getParameterTypes()) {
                            if (Publisher.class.isAssignableFrom(paramclazz)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            if (!m.isAnnotationPresent(BackpressureSupport.class)) {
                                b.append("No @BackpressureSupport annotation (has Publisher param): ").append(m).append("\r\n");
                            }
                        } else {
                            if (m.isAnnotationPresent(BackpressureSupport.class)) {
                                b.append("Unnecessary @BackpressureSupport annotation: ").append(m).append("\r\n");
                            }
                        }
                    }
                }
            }
        }
        if (b.length() != 0) {
            // System.out.println(clazz);
            // System.out.println("------------------------");
            // System.out.println(b);
            fail(b.toString());
        }
    }

    @Test
    public void checkReturnValueFlowable() {
        checkCheckReturnValueSupport(Flowable.class);
    }

    @Test
    public void checkReturnValueObservable() {
        checkCheckReturnValueSupport(Observable.class);
    }

    @Test
    public void checkReturnValueSingle() {
        checkCheckReturnValueSupport(Single.class);
    }

    @Test
    public void checkReturnValueCompletable() {
        checkCheckReturnValueSupport(Completable.class);
    }

    @Test
    public void checkReturnValueMaybe() {
        checkCheckReturnValueSupport(Maybe.class);
    }

    @Test
    public void checkReturnValueConnectableObservable() {
        checkCheckReturnValueSupport(ConnectableObservable.class);
    }

    @Test
    public void checkReturnValueConnectableFlowable() {
        checkCheckReturnValueSupport(ConnectableFlowable.class);
    }

    @Test
    public void checkReturnValueParallelFlowable() {
        checkCheckReturnValueSupport(ParallelFlowable.class);
    }

    @Test
    public void checkReturnValueAsyncSubject() {
        checkCheckReturnValueSupport(AsyncSubject.class);
    }

    @Test
    public void checkReturnValueBehaviorSubject() {
        checkCheckReturnValueSupport(BehaviorSubject.class);
    }

    @Test
    public void checkReturnValuePublishSubject() {
        checkCheckReturnValueSupport(PublishSubject.class);
    }

    @Test
    public void checkReturnValueReplaySubject() {
        checkCheckReturnValueSupport(ReplaySubject.class);
    }

    @Test
    public void checkReturnValueUnicastSubject() {
        checkCheckReturnValueSupport(UnicastSubject.class);
    }

    @Test
    public void checkReturnValueAsyncProcessor() {
        checkCheckReturnValueSupport(AsyncProcessor.class);
    }

    @Test
    public void checkReturnValueBehaviorProcessor() {
        checkCheckReturnValueSupport(BehaviorProcessor.class);
    }

    @Test
    public void checkReturnValuePublishProcessor() {
        checkCheckReturnValueSupport(PublishProcessor.class);
    }

    @Test
    public void checkReturnValueReplayProcessor() {
        checkCheckReturnValueSupport(ReplayProcessor.class);
    }

    @Test
    public void checkReturnValueUnicastProcessor() {
        checkCheckReturnValueSupport(UnicastProcessor.class);
    }

    @Test
    public void checkReturnValueMulticastProcessor() {
        checkCheckReturnValueSupport(MulticastProcessor.class);
    }

    @Test
    public void checkReturnValueSubject() {
        checkCheckReturnValueSupport(Subject.class);
    }

    @Test
    public void checkReturnValueFlowableProcessor() {
        checkCheckReturnValueSupport(FlowableProcessor.class);
    }

    @Test
    public void schedulerSupportFlowable() {
        checkSchedulerSupport(Flowable.class);
    }

    @Test
    public void schedulerSupportObservable() {
        checkSchedulerSupport(Observable.class);
    }

    @Test
    public void schedulerSupportSingle() {
        checkSchedulerSupport(Single.class);
    }

    @Test
    public void schedulerSupportCompletable() {
        checkSchedulerSupport(Completable.class);
    }

    @Test
    public void schedulerSupportMaybe() {
        checkSchedulerSupport(Maybe.class);
    }

    @Test
    public void schedulerSupportConnectableObservable() {
        checkSchedulerSupport(ConnectableObservable.class);
    }

    @Test
    public void schedulerSupportConnectableFlowable() {
        checkSchedulerSupport(ConnectableFlowable.class);
    }

    @Test
    public void schedulerSupportParallelFlowable() {
        checkSchedulerSupport(ParallelFlowable.class);
    }

    @Test
    public void backpressureSupportFlowable() {
        checkBackpressureSupport(Flowable.class);
    }

    @Test
    public void backpressureSupportObservable() {
        checkBackpressureSupport(Observable.class);
    }

    @Test
    public void backpressureSupportSingle() {
        checkBackpressureSupport(Single.class);
    }

    @Test
    public void backpressureSupportCompletable() {
        checkBackpressureSupport(Completable.class);
    }

    @Test
    public void backpressureSupportMaybe() {
        checkBackpressureSupport(Maybe.class);
    }

    @Test
    public void backpressureSupportConnectableFlowable() {
        checkBackpressureSupport(ConnectableFlowable.class);
    }

    @Test
    public void backpressureSupportConnectableObservable() {
        checkBackpressureSupport(ConnectableObservable.class);
    }

    @Test
    public void backpressureSupportParallelFlowable() {
        checkBackpressureSupport(ParallelFlowable.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BaseTypeAnnotations instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueFlowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueObservable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueObservable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueSingle() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueSingle);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueCompletable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueCompletable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueMaybe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueMaybe);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueConnectableObservable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueConnectableObservable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueConnectableFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueConnectableFlowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueParallelFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueParallelFlowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueAsyncSubject() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueAsyncSubject);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueBehaviorSubject() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueBehaviorSubject);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValuePublishSubject() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValuePublishSubject);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueReplaySubject() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueReplaySubject);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueUnicastSubject() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueUnicastSubject);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueAsyncProcessor() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueAsyncProcessor);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueBehaviorProcessor() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueBehaviorProcessor);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValuePublishProcessor() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValuePublishProcessor);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueReplayProcessor() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueReplayProcessor);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueUnicastProcessor() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueUnicastProcessor);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueMulticastProcessor() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueMulticastProcessor);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueSubject() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueSubject);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkReturnValueFlowableProcessor() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.checkReturnValueFlowableProcessor);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulerSupportFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.schedulerSupportFlowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulerSupportObservable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.schedulerSupportObservable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulerSupportSingle() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.schedulerSupportSingle);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulerSupportCompletable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.schedulerSupportCompletable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulerSupportMaybe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.schedulerSupportMaybe);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulerSupportConnectableObservable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.schedulerSupportConnectableObservable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulerSupportConnectableFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.schedulerSupportConnectableFlowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulerSupportParallelFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.schedulerSupportParallelFlowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureSupportFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.backpressureSupportFlowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureSupportObservable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.backpressureSupportObservable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureSupportSingle() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.backpressureSupportSingle);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureSupportCompletable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.backpressureSupportCompletable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureSupportMaybe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.backpressureSupportMaybe);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureSupportConnectableFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.backpressureSupportConnectableFlowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureSupportConnectableObservable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.backpressureSupportConnectableObservable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureSupportParallelFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.backpressureSupportParallelFlowable);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> payload) throws java.lang.Throwable {
            this.instance = new BaseTypeAnnotations();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueFlowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueObservable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueSingle;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueCompletable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueMaybe;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueConnectableObservable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueConnectableFlowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueParallelFlowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueAsyncSubject;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueBehaviorSubject;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValuePublishSubject;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueReplaySubject;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueUnicastSubject;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueAsyncProcessor;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueBehaviorProcessor;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValuePublishProcessor;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueReplayProcessor;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueUnicastProcessor;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueMulticastProcessor;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueSubject;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> checkReturnValueFlowableProcessor;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> schedulerSupportFlowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> schedulerSupportObservable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> schedulerSupportSingle;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> schedulerSupportCompletable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> schedulerSupportMaybe;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> schedulerSupportConnectableObservable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> schedulerSupportConnectableFlowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> schedulerSupportParallelFlowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> backpressureSupportFlowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> backpressureSupportObservable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> backpressureSupportSingle;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> backpressureSupportCompletable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> backpressureSupportMaybe;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> backpressureSupportConnectableFlowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> backpressureSupportConnectableObservable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BaseTypeAnnotations> backpressureSupportParallelFlowable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.checkReturnValueFlowable = BaseTypeAnnotations::checkReturnValueFlowable;
            this.payloads.checkReturnValueObservable = BaseTypeAnnotations::checkReturnValueObservable;
            this.payloads.checkReturnValueSingle = BaseTypeAnnotations::checkReturnValueSingle;
            this.payloads.checkReturnValueCompletable = BaseTypeAnnotations::checkReturnValueCompletable;
            this.payloads.checkReturnValueMaybe = BaseTypeAnnotations::checkReturnValueMaybe;
            this.payloads.checkReturnValueConnectableObservable = BaseTypeAnnotations::checkReturnValueConnectableObservable;
            this.payloads.checkReturnValueConnectableFlowable = BaseTypeAnnotations::checkReturnValueConnectableFlowable;
            this.payloads.checkReturnValueParallelFlowable = BaseTypeAnnotations::checkReturnValueParallelFlowable;
            this.payloads.checkReturnValueAsyncSubject = BaseTypeAnnotations::checkReturnValueAsyncSubject;
            this.payloads.checkReturnValueBehaviorSubject = BaseTypeAnnotations::checkReturnValueBehaviorSubject;
            this.payloads.checkReturnValuePublishSubject = BaseTypeAnnotations::checkReturnValuePublishSubject;
            this.payloads.checkReturnValueReplaySubject = BaseTypeAnnotations::checkReturnValueReplaySubject;
            this.payloads.checkReturnValueUnicastSubject = BaseTypeAnnotations::checkReturnValueUnicastSubject;
            this.payloads.checkReturnValueAsyncProcessor = BaseTypeAnnotations::checkReturnValueAsyncProcessor;
            this.payloads.checkReturnValueBehaviorProcessor = BaseTypeAnnotations::checkReturnValueBehaviorProcessor;
            this.payloads.checkReturnValuePublishProcessor = BaseTypeAnnotations::checkReturnValuePublishProcessor;
            this.payloads.checkReturnValueReplayProcessor = BaseTypeAnnotations::checkReturnValueReplayProcessor;
            this.payloads.checkReturnValueUnicastProcessor = BaseTypeAnnotations::checkReturnValueUnicastProcessor;
            this.payloads.checkReturnValueMulticastProcessor = BaseTypeAnnotations::checkReturnValueMulticastProcessor;
            this.payloads.checkReturnValueSubject = BaseTypeAnnotations::checkReturnValueSubject;
            this.payloads.checkReturnValueFlowableProcessor = BaseTypeAnnotations::checkReturnValueFlowableProcessor;
            this.payloads.schedulerSupportFlowable = BaseTypeAnnotations::schedulerSupportFlowable;
            this.payloads.schedulerSupportObservable = BaseTypeAnnotations::schedulerSupportObservable;
            this.payloads.schedulerSupportSingle = BaseTypeAnnotations::schedulerSupportSingle;
            this.payloads.schedulerSupportCompletable = BaseTypeAnnotations::schedulerSupportCompletable;
            this.payloads.schedulerSupportMaybe = BaseTypeAnnotations::schedulerSupportMaybe;
            this.payloads.schedulerSupportConnectableObservable = BaseTypeAnnotations::schedulerSupportConnectableObservable;
            this.payloads.schedulerSupportConnectableFlowable = BaseTypeAnnotations::schedulerSupportConnectableFlowable;
            this.payloads.schedulerSupportParallelFlowable = BaseTypeAnnotations::schedulerSupportParallelFlowable;
            this.payloads.backpressureSupportFlowable = BaseTypeAnnotations::backpressureSupportFlowable;
            this.payloads.backpressureSupportObservable = BaseTypeAnnotations::backpressureSupportObservable;
            this.payloads.backpressureSupportSingle = BaseTypeAnnotations::backpressureSupportSingle;
            this.payloads.backpressureSupportCompletable = BaseTypeAnnotations::backpressureSupportCompletable;
            this.payloads.backpressureSupportMaybe = BaseTypeAnnotations::backpressureSupportMaybe;
            this.payloads.backpressureSupportConnectableFlowable = BaseTypeAnnotations::backpressureSupportConnectableFlowable;
            this.payloads.backpressureSupportConnectableObservable = BaseTypeAnnotations::backpressureSupportConnectableObservable;
            this.payloads.backpressureSupportParallelFlowable = BaseTypeAnnotations::backpressureSupportParallelFlowable;
        }
    }
}
