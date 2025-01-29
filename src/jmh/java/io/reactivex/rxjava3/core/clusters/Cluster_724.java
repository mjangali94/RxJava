package io.reactivex.rxjava3.core.clusters;

public class Cluster_724 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_724() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.reduceWithSeedReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.collectInitialSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.toListSupplierReturnsNullSingle.evaluate();
            this._Benchmark_benchmark_3.payloads.toMapMapSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_3.payloads.toMultimapMapSupplierReturnsNull.evaluate();
        }

   }

}