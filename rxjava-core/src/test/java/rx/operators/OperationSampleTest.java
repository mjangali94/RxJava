/**
 * Copyright 2014 Netflix, Inc.
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
package rx.operators;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import rx.Observable;
import rx.Observer;
import rx.Scheduler.Inner;
import rx.Subscription;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;

public class OperationSampleTest {
    private TestScheduler scheduler;
    private Observer<Long> observer;
    private Observer<Object> observer2;

    @Before
    @SuppressWarnings("unchecked")
    // due to mocking
    public void before() {
        scheduler = new TestScheduler();
        observer = mock(Observer.class);
        observer2 = mock(Observer.class);
    }

    @Test
    public void testSample() {
        Observable<Long> source = Observable.create(new Observable.OnSubscribeFunc<Long>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Long> observer1) {
                scheduler.schedule(new Action1<Inner>() {
                    @Override
                    public void call(Inner inner) {
                        observer1.onNext(1L);
                    }
                }, 1, TimeUnit.SECONDS);
                scheduler.schedule(new Action1<Inner>() {
                    @Override
                    public void call(Inner inner) {
                        observer1.onNext(2L);
                    }
                }, 2, TimeUnit.SECONDS);
                scheduler.schedule(new Action1<Inner>() {
                    @Override
                    public void call(Inner inner) {
                        observer1.onCompleted();
                    }
                }, 3, TimeUnit.SECONDS);

                return Subscriptions.empty();
            }
        });

        Observable<Long> sampled = Observable.create(OperationSample.sample(source, 400L, TimeUnit.MILLISECONDS, scheduler));
        sampled.subscribe(observer);

        InOrder inOrder = inOrder(observer);

        scheduler.advanceTimeTo(800L, TimeUnit.MILLISECONDS);
        verify(observer, never()).onNext(any(Long.class));
        verify(observer, never()).onCompleted();
        verify(observer, never()).onError(any(Throwable.class));

        scheduler.advanceTimeTo(1200L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(1L);
        verify(observer, never()).onNext(2L);
        verify(observer, never()).onCompleted();
        verify(observer, never()).onError(any(Throwable.class));

        scheduler.advanceTimeTo(1600L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(1L);
        verify(observer, never()).onNext(2L);
        verify(observer, never()).onCompleted();
        verify(observer, never()).onError(any(Throwable.class));

        scheduler.advanceTimeTo(2000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(1L);
        inOrder.verify(observer, times(1)).onNext(2L);
        verify(observer, never()).onCompleted();
        verify(observer, never()).onError(any(Throwable.class));

        scheduler.advanceTimeTo(3000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(1L);
        inOrder.verify(observer, times(2)).onNext(2L);
        verify(observer, times(1)).onCompleted();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerNormal() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();

        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);

        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        source.onNext(3);
        source.onNext(4);
        sampler.onNext(2);
        source.onCompleted();
        sampler.onNext(3);

        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, never()).onNext(1);
        inOrder.verify(observer2, times(1)).onNext(2);
        inOrder.verify(observer2, never()).onNext(3);
        inOrder.verify(observer2, times(1)).onNext(4);
        inOrder.verify(observer2, times(1)).onCompleted();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerNoDuplicates() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();

        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);

        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        sampler.onNext(1);

        source.onNext(3);
        source.onNext(4);
        sampler.onNext(2);
        sampler.onNext(2);

        source.onCompleted();
        sampler.onNext(3);

        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, never()).onNext(1);
        inOrder.verify(observer2, times(1)).onNext(2);
        inOrder.verify(observer2, never()).onNext(3);
        inOrder.verify(observer2, times(1)).onNext(4);
        inOrder.verify(observer2, times(1)).onCompleted();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerTerminatingEarly() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();

        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);

        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        sampler.onCompleted();

        source.onNext(3);
        source.onNext(4);

        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, never()).onNext(1);
        inOrder.verify(observer2, times(1)).onNext(2);
        inOrder.verify(observer2, times(1)).onCompleted();
        inOrder.verify(observer2, never()).onNext(any());
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerEmitAndTerminate() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();

        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);

        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        source.onNext(3);
        source.onCompleted();
        sampler.onNext(2);
        sampler.onCompleted();

        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, never()).onNext(1);
        inOrder.verify(observer2, times(1)).onNext(2);
        inOrder.verify(observer2, never()).onNext(3);
        inOrder.verify(observer2, times(1)).onCompleted();
        inOrder.verify(observer2, never()).onNext(any());
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerEmptySource() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();

        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);

        source.onCompleted();
        sampler.onNext(1);

        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, times(1)).onCompleted();
        verify(observer2, never()).onNext(any());
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerSourceThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();

        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);

        source.onNext(1);
        source.onError(new RuntimeException("Forced failure!"));
        sampler.onNext(1);

        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, times(1)).onError(any(Throwable.class));
        verify(observer2, never()).onNext(any());
        verify(observer, never()).onCompleted();
    }

    @Test
    public void sampleWithSamplerThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();

        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);

        source.onNext(1);
        sampler.onNext(1);
        sampler.onError(new RuntimeException("Forced failure!"));

        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, times(1)).onNext(1);
        inOrder.verify(observer2, times(1)).onError(any(RuntimeException.class));
        verify(observer, never()).onCompleted();
    }
}
