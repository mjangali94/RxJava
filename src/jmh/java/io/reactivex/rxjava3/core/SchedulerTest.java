/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.rxjava3.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SchedulerTest {

    @Test
    public void clockDriftCalculation() {
        assertEquals(100_000_000L, Scheduler.computeClockDrift(100, "milliseconds"));
        assertEquals(2_000_000_000L, Scheduler.computeClockDrift(2, "seconds"));
        assertEquals(180_000_000_000L, Scheduler.computeClockDrift(3, "minutes"));
        assertEquals(240_000_000_000L, Scheduler.computeClockDrift(4, "random"));
        assertEquals(300_000_000_000L, Scheduler.computeClockDrift(5, null));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class myBenchmark extends JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clockDriftCalculation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::clockDriftCalculation, this.description("clockDriftCalculation"));
        }

        private SchedulerTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SchedulerTest();
        }

        @java.lang.Override
        public SchedulerTest implementation() {
            return this.implementation;
        }
    }
}
