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

package io.reactivex.rxjava3.internal.jdk8;

import java.util.concurrent.CompletableFuture;

import org.junit.Test; import org.junit.Rule; import io.reactivex.rxjava3.core.PerformanceLogger;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleFromCompletionStageTest extends RxJavaTest {

	@Rule
	public PerformanceLogger myPLogger = new PerformanceLogger();

    @Test
    public void syncSuccess() {
        Single.fromCompletionStage(CompletableFuture.completedFuture(1))
        .test()
        .assertResult(1);
    }

    @Test
    public void syncFailure() {
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        cf.completeExceptionally(new TestException());

        Single.fromCompletionStage(cf)
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void syncNull() {
        Single.fromCompletionStage(CompletableFuture.<Integer>completedFuture(null))
        .test()
        .assertFailure(NullPointerException.class);
    }

    @Test
    public void dispose() {
        CompletableFuture<Integer> cf = new CompletableFuture<>();

        TestObserver<Integer> to = Single.fromCompletionStage(cf)
        .test();

        to.assertEmpty();

        to.dispose();

        cf.complete(1);

        to.assertEmpty();
    }

    @Test
    public void dispose2() {
        TestHelper.checkDisposed(Single.fromCompletionStage(new CompletableFuture<>()));
    }
}
