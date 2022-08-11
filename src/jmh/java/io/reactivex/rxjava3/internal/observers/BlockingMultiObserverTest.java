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
package io.reactivex.rxjava3.internal.observers;

import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BlockingMultiObserverTest extends RxJavaTest {

    @Test
    public void dispose() {
        BlockingMultiObserver<Integer> bmo = new BlockingMultiObserver<>();
        bmo.dispose();
        Disposable d = Disposable.empty();
        bmo.onSubscribe(d);
    }

    @Test
    public void blockingGetDefault() {
        final BlockingMultiObserver<Integer> bmo = new BlockingMultiObserver<>();
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                bmo.onSuccess(1);
            }
        }, 100, TimeUnit.MILLISECONDS);
        assertEquals(1, bmo.blockingGet(0).intValue());
    }

    @Test
    public void blockingAwait() {
        final BlockingMultiObserver<Integer> bmo = new BlockingMultiObserver<>();
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                bmo.onSuccess(1);
            }
        }, 100, TimeUnit.MILLISECONDS);
        assertTrue(bmo.blockingAwait(1, TimeUnit.MINUTES));
    }

    @Test
    public void blockingGetDefaultInterrupt() {
        final BlockingMultiObserver<Integer> bmo = new BlockingMultiObserver<>();
        Thread.currentThread().interrupt();
        try {
            bmo.blockingGet(0);
            fail("Should have thrown");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof InterruptedException);
        } finally {
            Thread.interrupted();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BlockingMultiObserverTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingGetDefault() throws java.lang.Throwable {
            this.payloads.blockingGetDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingAwait() throws java.lang.Throwable {
            this.payloads.blockingAwait.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingGetDefaultInterrupt() throws java.lang.Throwable {
            this.payloads.blockingGetDefaultInterrupt.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingMultiObserverTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingMultiObserverTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingMultiObserverTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingMultiObserverTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BlockingMultiObserverTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingMultiObserverTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BlockingMultiObserverTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BlockingMultiObserverTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement blockingGetDefault;

            public org.junit.runners.model.Statement blockingAwait;

            public org.junit.runners.model.Statement blockingGetDefaultInterrupt;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.dispose = _ClassStatement.forPayload(BlockingMultiObserverTest::dispose, "dispose", this);
            this.payloads.blockingGetDefault = _ClassStatement.forPayload(BlockingMultiObserverTest::blockingGetDefault, "blockingGetDefault", this);
            this.payloads.blockingAwait = _ClassStatement.forPayload(BlockingMultiObserverTest::blockingAwait, "blockingAwait", this);
            this.payloads.blockingGetDefaultInterrupt = _ClassStatement.forPayload(BlockingMultiObserverTest::blockingGetDefaultInterrupt, "blockingGetDefaultInterrupt", this);
        }
    }
}
