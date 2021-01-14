/**
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
package io.reactivex.rxjava3.flowable;

import static org.mockito.Mockito.inOrder;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableThrottleLastTests extends RxJavaTest {

    @Test
    public void throttle() {
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        TestScheduler s = new TestScheduler();
        PublishProcessor<Integer> o = PublishProcessor.create();
        o.throttleLast(500, TimeUnit.MILLISECONDS, s).subscribe(subscriber);
        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(1);
        // deliver
        o.onNext(2);
        s.advanceTimeTo(501, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(3);
        s.advanceTimeTo(600, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(4);
        s.advanceTimeTo(700, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(5);
        // deliver
        o.onNext(6);
        s.advanceTimeTo(1001, TimeUnit.MILLISECONDS);
        // deliver
        o.onNext(7);
        s.advanceTimeTo(1501, TimeUnit.MILLISECONDS);
        o.onComplete();
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onNext(2);
        inOrder.verify(subscriber).onNext(6);
        inOrder.verify(subscriber).onNext(7);
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttle, this.description("throttle"));
        }

        private FlowableThrottleLastTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableThrottleLastTests();
        }

        @java.lang.Override
        public FlowableThrottleLastTests implementation() {
            return this.implementation;
        }
    }
}
