/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;

import java.io.FileWriter;
import java.io.IOException;
import org.junit.rules.TestName;
import org.junit.Test;
import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableOnBackpressureErrorTest extends RxJavaTest {

    @org.junit.Rule public TestName name = new TestName();
    @org.junit.Before
    public void myBefore() throws IOException {
    	FileWriter fw = new FileWriter("/Users/massi/Desktop/tmp.csv", true);
    	fw.write(this.getClass().getName()+"."+name.getMethodName() +","+io.reactivex.rxjava3.core.myTestLogger.hitting_count()+"\n");
    	fw.close();
    }
@Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).toFlowable(BackpressureStrategy.ERROR));
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Observable.just(1).toFlowable(BackpressureStrategy.ERROR));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {
            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return new FlowableOnBackpressureError<>(f);
            }
        });
    }

    @Test
    public void badSource() {
        TestHelper.<Integer>checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {
            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return new FlowableOnBackpressureError<>(f);
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void overflowCancels() {
        PublishSubject<Integer> ps = PublishSubject.create();

        TestSubscriber<Integer> ts = ps.toFlowable(BackpressureStrategy.ERROR)
        .test(0L);

        assertTrue(ps.hasObservers());

        ps.onNext(1);

        assertFalse(ps.hasObservers());

        ts.assertFailure(MissingBackpressureException.class);
    }
}
