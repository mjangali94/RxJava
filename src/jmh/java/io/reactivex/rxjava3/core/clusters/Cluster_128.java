package io.reactivex.rxjava3.core.clusters;

public class Cluster_128 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeUntilTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.observers.InnerQueuedObserverTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.disposables.SequentialDisposableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableGroupByTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeUntilTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.observers.InnerQueuedObserverTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.disposables.SequentialDisposableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableGroupByTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_128() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.singleError.evaluate();
            this._Benchmark_benchmark_1.payloads.nonNullConnection.evaluate();
            this._Benchmark_benchmark_2.payloads.toFutureWithException.evaluate();
            this._Benchmark_benchmark_3.payloads.untilPublisherDispose.evaluate();
            this._Benchmark_benchmark_4.payloads.switchShouldTriggerUnsubscribe.evaluate();
            this._Benchmark_benchmark_5.payloads.dispose.evaluate();
            this._Benchmark_benchmark_6.payloads.unsubscribingWithoutUnderlyingDoesNothing.evaluate();
            this._Benchmark_benchmark_7.payloads.disposed.evaluate();
            this._Benchmark_benchmark_8.payloads.groupByWithElementSelector2.evaluate();
        }

   }

}