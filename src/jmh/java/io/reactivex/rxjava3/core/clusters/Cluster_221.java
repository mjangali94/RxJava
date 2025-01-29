package io.reactivex.rxjava3.core.clusters;

public class Cluster_221 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_221() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.zipWithIterableIteratorNull.evaluate();
            this._Benchmark_benchmark_0.payloads.scanSeedSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.distinctSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_3.payloads.hasNextThrowsImmediately.evaluate();
            this._Benchmark_benchmark_0.payloads.repeatWhenFunctionReturnsNull.evaluate();
        }

   }

}