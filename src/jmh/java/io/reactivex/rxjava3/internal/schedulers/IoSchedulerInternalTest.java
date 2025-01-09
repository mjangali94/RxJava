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
package io.reactivex.rxjava3.internal.schedulers;

import static org.junit.Assert.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.internal.schedulers.IoScheduler.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class IoSchedulerInternalTest extends RxJavaTest {

    @Test
    public void expiredQueueEmpty() {
        ConcurrentLinkedQueue<ThreadWorker> expire = new ConcurrentLinkedQueue<>();
        CompositeDisposable cd = new CompositeDisposable();
        CachedWorkerPool.evictExpiredWorkers(expire, cd);
    }

    @Test
    public void expiredWorkerRemoved() {
        ConcurrentLinkedQueue<ThreadWorker> expire = new ConcurrentLinkedQueue<>();
        CompositeDisposable cd = new CompositeDisposable();
        ThreadWorker tw = new ThreadWorker(new RxThreadFactory("IoExpiryTest"));
        try {
            expire.add(tw);
            cd.add(tw);
            CachedWorkerPool.evictExpiredWorkers(expire, cd);
            assertTrue(tw.isDisposed());
            assertTrue(expire.isEmpty());
        } finally {
            tw.dispose();
        }
    }

    @Test
    public void noExpiredWorker() {
        ConcurrentLinkedQueue<ThreadWorker> expire = new ConcurrentLinkedQueue<>();
        CompositeDisposable cd = new CompositeDisposable();
        ThreadWorker tw = new ThreadWorker(new RxThreadFactory("IoExpiryTest"));
        tw.setExpirationTime(System.nanoTime() + 10_000_000_000L);
        try {
            expire.add(tw);
            cd.add(tw);
            CachedWorkerPool.evictExpiredWorkers(expire, cd);
            assertFalse(tw.isDisposed());
            assertFalse(expire.isEmpty());
        } finally {
            tw.dispose();
        }
    }

    @Test
    public void expireReuseRace() {
        ConcurrentLinkedQueue<ThreadWorker> expire = new ConcurrentLinkedQueue<>();
        CompositeDisposable cd = new CompositeDisposable();
        ThreadWorker tw = new ThreadWorker(new RxThreadFactory("IoExpiryTest"));
        tw.dispose();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            expire.add(tw);
            cd.add(tw);
            TestHelper.race(() -> CachedWorkerPool.evictExpiredWorkers(expire, cd), () -> expire.remove(tw));
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public IoSchedulerInternalTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_expiredQueueEmpty() throws java.lang.Throwable {
            this.payloads.expiredQueueEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_expiredWorkerRemoved() throws java.lang.Throwable {
            this.payloads.expiredWorkerRemoved.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noExpiredWorker() throws java.lang.Throwable {
            this.payloads.noExpiredWorker.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_expireReuseRace() throws java.lang.Throwable {
            this.payloads.expireReuseRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<IoSchedulerInternalTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<IoSchedulerInternalTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<IoSchedulerInternalTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<IoSchedulerInternalTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new IoSchedulerInternalTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<IoSchedulerInternalTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(IoSchedulerInternalTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(IoSchedulerInternalTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement expiredQueueEmpty;

            public org.junit.runners.model.Statement expiredWorkerRemoved;

            public org.junit.runners.model.Statement noExpiredWorker;

            public org.junit.runners.model.Statement expireReuseRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.expiredQueueEmpty = _ClassStatement.forPayload(IoSchedulerInternalTest::expiredQueueEmpty, "expiredQueueEmpty", this);
            this.payloads.expiredWorkerRemoved = _ClassStatement.forPayload(IoSchedulerInternalTest::expiredWorkerRemoved, "expiredWorkerRemoved", this);
            this.payloads.noExpiredWorker = _ClassStatement.forPayload(IoSchedulerInternalTest::noExpiredWorker, "noExpiredWorker", this);
            this.payloads.expireReuseRace = _ClassStatement.forPayload(IoSchedulerInternalTest::expireReuseRace, "expireReuseRace", this);
        }
    }
}
