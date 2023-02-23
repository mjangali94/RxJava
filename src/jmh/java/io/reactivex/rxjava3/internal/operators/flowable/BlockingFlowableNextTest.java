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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.SerialDisposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableNext.NextSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class BlockingFlowableNextTest extends RxJavaTest {

    private void fireOnNextInNewThread(final FlowableProcessor<String> o, final String value) {
        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                // ignore
                }
                o.onNext(value);
            }
        }.start();
    }

    private void fireOnErrorInNewThread(final FlowableProcessor<String> o) {
        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                // ignore
                }
                o.onError(new TestException());
            }
        }.start();
    }

    @Test
    public void next() {
        FlowableProcessor<String> obs = PublishProcessor.create();
        Iterator<String> it = obs.blockingNext().iterator();
        fireOnNextInNewThread(obs, "one");
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        fireOnNextInNewThread(obs, "two");
        assertTrue(it.hasNext());
        assertEquals("two", it.next());
        fireOnNextInNewThread(obs, "three");
        try {
            assertEquals("three", it.next());
        } catch (NoSuchElementException e) {
            fail("Calling next() without hasNext() should wait for next fire");
        }
        obs.onComplete();
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("At the end of an iterator should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
        // If the observable is completed, hasNext always returns false and next always throw a NoSuchElementException.
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("At the end of an iterator should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void nextWithError() {
        FlowableProcessor<String> obs = PublishProcessor.create();
        Iterator<String> it = obs.blockingNext().iterator();
        fireOnNextInNewThread(obs, "one");
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        fireOnErrorInNewThread(obs);
        try {
            it.hasNext();
            fail("Expected an TestException");
        } catch (TestException e) {
        }
        assertErrorAfterObservableFail(it);
    }

    @Test
    public void nextWithEmpty() {
        Flowable<String> obs = Flowable.<String>empty().observeOn(Schedulers.newThread());
        Iterator<String> it = obs.blockingNext().iterator();
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("At the end of an iterator should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
        // If the observable is completed, hasNext always returns false and next always throw a NoSuchElementException.
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("At the end of an iterator should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void onError() throws Throwable {
        FlowableProcessor<String> obs = PublishProcessor.create();
        Iterator<String> it = obs.blockingNext().iterator();
        obs.onError(new TestException());
        try {
            it.hasNext();
            fail("Expected an TestException");
        } catch (TestException e) {
        // successful
        }
        assertErrorAfterObservableFail(it);
    }

    @Test
    public void onErrorInNewThread() {
        FlowableProcessor<String> obs = PublishProcessor.create();
        Iterator<String> it = obs.blockingNext().iterator();
        fireOnErrorInNewThread(obs);
        try {
            it.hasNext();
            fail("Expected an TestException");
        } catch (TestException e) {
        // successful
        }
        assertErrorAfterObservableFail(it);
    }

    private void assertErrorAfterObservableFail(Iterator<String> it) {
        // After the observable fails, hasNext and next always throw the exception.
        try {
            it.hasNext();
            fail("hasNext should throw a TestException");
        } catch (TestException e) {
        }
        try {
            it.next();
            fail("next should throw a TestException");
        } catch (TestException e) {
        }
    }

    @Test
    public void nextWithOnlyUsingNextMethod() {
        FlowableProcessor<String> obs = PublishProcessor.create();
        Iterator<String> it = obs.blockingNext().iterator();
        fireOnNextInNewThread(obs, "one");
        assertEquals("one", it.next());
        fireOnNextInNewThread(obs, "two");
        assertEquals("two", it.next());
        obs.onComplete();
        try {
            it.next();
            fail("At the end of an iterator should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void nextWithCallingHasNextMultipleTimes() {
        FlowableProcessor<String> obs = PublishProcessor.create();
        Iterator<String> it = obs.blockingNext().iterator();
        fireOnNextInNewThread(obs, "one");
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        obs.onComplete();
        try {
            it.next();
            fail("At the end of an iterator should throw a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

    /**
     * Confirm that no buffering or blocking of the Observable onNext calls occurs and it just grabs the next emitted value.
     * <p>
     * This results in output such as {@code => a: 1 b: 2 c: 89}
     *
     * @throws Throwable some method call is declared throws
     */
    @Test
    public void noBufferingOrBlockingOfSequence() throws Throwable {
        int repeat = 0;
        for (; ; ) {
            final SerialDisposable task = new SerialDisposable();
            try {
                final CountDownLatch finished = new CountDownLatch(1);
                final int COUNT = 30;
                final CountDownLatch timeHasPassed = new CountDownLatch(COUNT);
                final AtomicBoolean running = new AtomicBoolean(true);
                final AtomicInteger count = new AtomicInteger(0);
                final Flowable<Integer> obs = Flowable.unsafeCreate(new Publisher<Integer>() {

                    @Override
                    public void subscribe(final Subscriber<? super Integer> subscriber) {
                        subscriber.onSubscribe(new BooleanSubscription());
                        task.replace(Schedulers.single().scheduleDirect(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    while (running.get() && !task.isDisposed()) {
                                        subscriber.onNext(count.incrementAndGet());
                                        timeHasPassed.countDown();
                                    }
                                    subscriber.onComplete();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                } finally {
                                    finished.countDown();
                                }
                            }
                        }));
                    }
                });
                Iterator<Integer> it = obs.blockingNext().iterator();
                assertTrue(it.hasNext());
                int a = it.next();
                assertTrue(it.hasNext());
                int b = it.next();
                // we should have a different value
                assertTrue("a and b should be different", a != b);
                // wait for some time (if times out we are blocked somewhere so fail ... set very high for very slow, constrained machines)
                timeHasPassed.await(8000, TimeUnit.MILLISECONDS);
                assertTrue(it.hasNext());
                int c = it.next();
                assertTrue("c should not just be the next in sequence", c != (b + 1));
                assertTrue("expected that c [" + c + "] is higher than or equal to " + COUNT, c >= COUNT);
                assertTrue(it.hasNext());
                int d = it.next();
                assertTrue(d > c);
                // shut down the thread
                running.set(false);
                finished.await();
                assertFalse(it.hasNext());
                // System.out.println("a: " + a + " b: " + b + " c: " + c);
                break;
            } catch (AssertionError ex) {
                if (++repeat == 3) {
                    throw ex;
                }
                Thread.sleep((int) (1000 * Math.pow(2, repeat - 1)));
            } finally {
                task.dispose();
            }
        }
    }

    @Test
    public void singleSourceManyIterators() throws InterruptedException {
        Flowable<Long> f = Flowable.interval(250, TimeUnit.MILLISECONDS);
        PublishProcessor<Integer> terminal = PublishProcessor.create();
        Flowable<Long> source = f.takeUntil(terminal);
        Iterable<Long> iter = source.blockingNext();
        for (int j = 0; j < 3; j++) {
            BlockingFlowableNext.NextIterator<Long> it = (BlockingFlowableNext.NextIterator<Long>) iter.iterator();
            for (long i = 0; i < 10; i++) {
                Assert.assertTrue(it.hasNext());
                Assert.assertEquals(j + "th iteration next", Long.valueOf(i), it.next());
            }
            terminal.onNext(1);
        }
    }

    @Test
    public void synchronousNext() {
        assertEquals(1, BehaviorProcessor.createDefault(1).take(1).blockingSingle().intValue());
        assertEquals(2, BehaviorProcessor.createDefault(2).blockingIterable().iterator().next().intValue());
        assertEquals(3, BehaviorProcessor.createDefault(3).blockingNext().iterator().next().intValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        Flowable.never().blockingNext().iterator().remove();
    }

    @Test
    public void interrupt() {
        Iterator<Object> it = Flowable.never().blockingNext().iterator();
        try {
            Thread.currentThread().interrupt();
            it.next();
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
    }

    @Test
    public void nextObserverError() {
        NextSubscriber<Integer> no = new NextSubscriber<>();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            no.onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void nextObserverOnNext() throws Exception {
        NextSubscriber<Integer> no = new NextSubscriber<>();
        no.setWaiting();
        no.onNext(Notification.createOnNext(1));
        no.setWaiting();
        no.onNext(Notification.createOnNext(1));
        assertEquals(1, no.takeNext().getValue().intValue());
    }

    @Test
    public void nextObserverOnCompleteOnNext() throws Exception {
        NextSubscriber<Integer> no = new NextSubscriber<>();
        no.setWaiting();
        no.onNext(Notification.<Integer>createOnComplete());
        no.setWaiting();
        no.onNext(Notification.createOnNext(1));
        assertTrue(no.takeNext().isOnComplete());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BlockingFlowableNextTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_next() throws java.lang.Throwable {
            this.payloads.next.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextWithError() throws java.lang.Throwable {
            this.payloads.nextWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextWithEmpty() throws java.lang.Throwable {
            this.payloads.nextWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onError() throws java.lang.Throwable {
            this.payloads.onError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorInNewThread() throws java.lang.Throwable {
            this.payloads.onErrorInNewThread.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextWithOnlyUsingNextMethod() throws java.lang.Throwable {
            this.payloads.nextWithOnlyUsingNextMethod.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextWithCallingHasNextMultipleTimes() throws java.lang.Throwable {
            this.payloads.nextWithCallingHasNextMultipleTimes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBufferingOrBlockingOfSequence() throws java.lang.Throwable {
            this.payloads.noBufferingOrBlockingOfSequence.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceManyIterators() throws java.lang.Throwable {
            this.payloads.singleSourceManyIterators.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_synchronousNext() throws java.lang.Throwable {
            this.payloads.synchronousNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_remove() throws java.lang.Throwable {
            this.payloads.remove.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interrupt() throws java.lang.Throwable {
            this.payloads.interrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextObserverError() throws java.lang.Throwable {
            this.payloads.nextObserverError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextObserverOnNext() throws java.lang.Throwable {
            this.payloads.nextObserverOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextObserverOnCompleteOnNext() throws java.lang.Throwable {
            this.payloads.nextObserverOnCompleteOnNext.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableNextTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableNextTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableNextTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableNextTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BlockingFlowableNextTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableNextTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BlockingFlowableNextTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BlockingFlowableNextTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement next;

            public org.junit.runners.model.Statement nextWithError;

            public org.junit.runners.model.Statement nextWithEmpty;

            public org.junit.runners.model.Statement onError;

            public org.junit.runners.model.Statement onErrorInNewThread;

            public org.junit.runners.model.Statement nextWithOnlyUsingNextMethod;

            public org.junit.runners.model.Statement nextWithCallingHasNextMultipleTimes;

            public org.junit.runners.model.Statement noBufferingOrBlockingOfSequence;

            public org.junit.runners.model.Statement singleSourceManyIterators;

            public org.junit.runners.model.Statement synchronousNext;

            public org.junit.runners.model.Statement remove;

            public org.junit.runners.model.Statement interrupt;

            public org.junit.runners.model.Statement nextObserverError;

            public org.junit.runners.model.Statement nextObserverOnNext;

            public org.junit.runners.model.Statement nextObserverOnCompleteOnNext;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.next = _ClassStatement.forPayload(BlockingFlowableNextTest::next, "next", this);
            this.payloads.nextWithError = _ClassStatement.forPayload(BlockingFlowableNextTest::nextWithError, "nextWithError", this);
            this.payloads.nextWithEmpty = _ClassStatement.forPayload(BlockingFlowableNextTest::nextWithEmpty, "nextWithEmpty", this);
            this.payloads.onError = _ClassStatement.forPayload(BlockingFlowableNextTest::onError, "onError", this);
            this.payloads.onErrorInNewThread = _ClassStatement.forPayload(BlockingFlowableNextTest::onErrorInNewThread, "onErrorInNewThread", this);
            this.payloads.nextWithOnlyUsingNextMethod = _ClassStatement.forPayload(BlockingFlowableNextTest::nextWithOnlyUsingNextMethod, "nextWithOnlyUsingNextMethod", this);
            this.payloads.nextWithCallingHasNextMultipleTimes = _ClassStatement.forPayload(BlockingFlowableNextTest::nextWithCallingHasNextMultipleTimes, "nextWithCallingHasNextMultipleTimes", this);
            this.payloads.noBufferingOrBlockingOfSequence = _ClassStatement.forPayload(BlockingFlowableNextTest::noBufferingOrBlockingOfSequence, "noBufferingOrBlockingOfSequence", this);
            this.payloads.singleSourceManyIterators = _ClassStatement.forPayload(BlockingFlowableNextTest::singleSourceManyIterators, "singleSourceManyIterators", this);
            this.payloads.synchronousNext = _ClassStatement.forPayload(BlockingFlowableNextTest::synchronousNext, "synchronousNext", this);
            this.payloads.remove = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableNextTest::remove, java.lang.UnsupportedOperationException.class), "remove", this);
            this.payloads.interrupt = _ClassStatement.forPayload(BlockingFlowableNextTest::interrupt, "interrupt", this);
            this.payloads.nextObserverError = _ClassStatement.forPayload(BlockingFlowableNextTest::nextObserverError, "nextObserverError", this);
            this.payloads.nextObserverOnNext = _ClassStatement.forPayload(BlockingFlowableNextTest::nextObserverOnNext, "nextObserverOnNext", this);
            this.payloads.nextObserverOnCompleteOnNext = _ClassStatement.forPayload(BlockingFlowableNextTest::nextObserverOnCompleteOnNext, "nextObserverOnCompleteOnNext", this);
        }
    }
}
