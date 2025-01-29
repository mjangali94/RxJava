package io.reactivex.rxjava3.core.clusters;

public class Cluster_40 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableTakeUntilTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDisposeOnTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableMergeIterableTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableCacheTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableToFutureTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapCompletableTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutWithSelectorTest._Benchmark _Benchmark_benchmark_11;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableTakeUntilTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDisposeOnTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMergeIterableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableCacheTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.completable.CompletableToFutureTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapCompletableTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutWithSelectorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_40() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_1.payloads.completeAfterCancel.evaluate();
            this._Benchmark_benchmark_2.payloads.cancelAfterHasNext.evaluate();
            this._Benchmark_benchmark_3.payloads.crossDispose.evaluate();
            this._Benchmark_benchmark_1.payloads.cancelDelayed.evaluate();
            this._Benchmark_benchmark_0.payloads.consumerDisposes.evaluate();
            this._Benchmark_benchmark_6.payloads.cancel2.evaluate();
            this._Benchmark_benchmark_6.payloads.cancel.evaluate();
            this._Benchmark_benchmark_8.payloads.synchronousDisconnect.evaluate();
            this._Benchmark_benchmark_9.payloads.undeliverableUponCancelDelayError.evaluate();
            this._Benchmark_benchmark_10.payloads.synchronousDisconnect.evaluate();
            this._Benchmark_benchmark_11.payloads.withOtherMainError.evaluate();
        }

   }

}