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

package io.reactivex.rxjava3.observable;

import static org.mockito.Mockito.inOrder;

import java.util.concurrent.TimeUnit;

import java.io.FileWriter;
import java.io.IOException;
import org.junit.rules.TestName;
import org.junit.Test;
import org.mockito.InOrder;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableThrottleLastTests extends RxJavaTest {

    @org.junit.Rule public TestName name = new TestName();
    @org.junit.Before
    public void myBefore() throws IOException {
    	FileWriter fw = new FileWriter("/Users/massi/Desktop/tmp.csv", true);
    	fw.write(this.getClass().getName()+"."+name.getMethodName() +","+io.reactivex.rxjava3.core.myTestLogger.hitting_count()+"\n");
    	fw.close();
    }
@Test
    public void throttle() {
        Observer<Integer> observer = TestHelper.mockObserver();

        TestScheduler s = new TestScheduler();
        PublishSubject<Integer> o = PublishSubject.create();
        o.throttleLast(500, TimeUnit.MILLISECONDS, s).subscribe(observer);

        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        o.onNext(1); // skip
        o.onNext(2); // deliver
        s.advanceTimeTo(501, TimeUnit.MILLISECONDS);
        o.onNext(3); // skip
        s.advanceTimeTo(600, TimeUnit.MILLISECONDS);
        o.onNext(4); // skip
        s.advanceTimeTo(700, TimeUnit.MILLISECONDS);
        o.onNext(5); // skip
        o.onNext(6); // deliver
        s.advanceTimeTo(1001, TimeUnit.MILLISECONDS);
        o.onNext(7); // deliver
        s.advanceTimeTo(1501, TimeUnit.MILLISECONDS);
        o.onComplete();

        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onNext(2);
        inOrder.verify(observer).onNext(6);
        inOrder.verify(observer).onNext(7);
        inOrder.verify(observer).onComplete();
        inOrder.verifyNoMoreInteractions();
    }
}
