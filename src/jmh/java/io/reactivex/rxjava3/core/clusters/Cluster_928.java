package io.reactivex.rxjava3.core.clusters;

public class Cluster_928 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableRetryTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapSingleTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapBiSelectorTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithObservableTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRetryTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapSingleTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapBiSelectorTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithObservableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_928() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.shouldDisposeInnerObservable.evaluate();
            this._Benchmark_benchmark_1.payloads.innerDisposedOnMainError.evaluate();
            this._Benchmark_benchmark_2.payloads.innerError.evaluate();
            this._Benchmark_benchmark_2.payloads.scalarSource.evaluate();
            this._Benchmark_benchmark_4.payloads.mapperCancels.evaluate();
            this._Benchmark_benchmark_5.payloads.boundaryDispose.evaluate();
        }

   }

}