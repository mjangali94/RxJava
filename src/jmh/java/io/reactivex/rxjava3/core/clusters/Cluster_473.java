package io.reactivex.rxjava3.core.clusters;

public class Cluster_473 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapBiSelectorTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapBiSelectorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_473() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.flatMapValueErrorThrown.evaluate();
            this._Benchmark_benchmark_0.payloads.flatMapValueNull.evaluate();
            this._Benchmark_benchmark_0.payloads.flatMapError.evaluate();
            this._Benchmark_benchmark_3.payloads.concatMapError.evaluate();
            this._Benchmark_benchmark_4.payloads.normal.evaluate();
            this._Benchmark_benchmark_4.payloads.resultSelectorReturnsNull.evaluate();
        }

   }

}