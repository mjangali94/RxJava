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

import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.junit.rules.Timeout;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.reactivex.rxjava3.testsupport.SuppressUndeliverableRule;

public abstract class RxJavaTest {



    @Rule
    public final SuppressUndeliverableRule suppressUndeliverableRule = new SuppressUndeliverableRule();

    /**
     * Announce creates a log print preventing Travis CI from killing the build.
     */
    @Test
    @Ignore
    public final void announce() {
    }

    @BenchmarkMode({Mode.Throughput,Mode.SampleTime})
    @Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 30, time = 1, timeUnit = TimeUnit.SECONDS)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value = 1)
    @State(Scope.Thread)
    public static abstract class myBenchmark extends JU2JmhBenchmark {

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().suppressUndeliverableRule, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        @java.lang.Override
        public abstract void createImplementation() throws java.lang.Throwable;

        @java.lang.Override
        public abstract RxJavaTest implementation();
    }
}
