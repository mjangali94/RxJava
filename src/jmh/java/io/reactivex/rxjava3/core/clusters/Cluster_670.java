package io.reactivex.rxjava3.core.clusters;

public class Cluster_670 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromCompletableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableHideTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableLatestTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapCompletableTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDebounceTest._Benchmark _Benchmark_benchmark_13;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromCompletableTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.completable.CompletableHideTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableLatestTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDebounceTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_670() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.unsubscribesFromUpstream.evaluate();
            this._Benchmark_benchmark_1.payloads.fromCompletable.evaluate();
            this._Benchmark_benchmark_1.payloads.fromCompletableInvokesLazy.evaluate();
            this._Benchmark_benchmark_1.payloads.fromCompletableTwice.evaluate();
            this._Benchmark_benchmark_0.payloads.unsubscribesFromUpstreamObservable.evaluate();
            this._Benchmark_benchmark_5.payloads.complete.evaluate();
            this._Benchmark_benchmark_6.payloads.remove.evaluate();
            this._Benchmark_benchmark_1.payloads.cancelWhileRunning.evaluate();
            this._Benchmark_benchmark_8.payloads.emptyScalarSource.evaluate();
            this._Benchmark_benchmark_9.payloads.rejectedFusionDelayError.evaluate();
            this._Benchmark_benchmark_10.payloads.getAfterCancel.evaluate();
            this._Benchmark_benchmark_10.payloads.getWithTimeoutAfterCancel.evaluate();
            this._Benchmark_benchmark_8.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_13.payloads.debounceOnEmpty.evaluate();
        }

   }

}