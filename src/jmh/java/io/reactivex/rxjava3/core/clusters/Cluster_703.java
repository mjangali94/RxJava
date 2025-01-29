package io.reactivex.rxjava3.core.clusters;

public class Cluster_703 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_703() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelActive.evaluate();
            this._Benchmark_benchmark_0.payloads.arrayDelayErrorDefault.evaluate();
            this._Benchmark_benchmark_2.payloads.onErrorReturnFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_2.payloads.distinctSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.unsubscribeFromSynchronousInfiniteObservable.evaluate();
            this._Benchmark_benchmark_2.payloads.publishFunctionReturnsNull.evaluate();
        }

   }

}