package io.reactivex.rxjava3.core.clusters;

public class Cluster_472 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleMergeTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleMergeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_472() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.concatMapValueDifferentType.evaluate();
            this._Benchmark_benchmark_0.payloads.concatMapValue.evaluate();
            this._Benchmark_benchmark_2.payloads.flatMapValueDifferentType.evaluate();
            this._Benchmark_benchmark_2.payloads.flatMapValue.evaluate();
            this._Benchmark_benchmark_4.payloads.mergeSingleSingle.evaluate();
            this._Benchmark_benchmark_0.payloads.mappedSingleOnError.evaluate();
            this._Benchmark_benchmark_2.payloads.mappedSingleOnError.evaluate();
            this._Benchmark_benchmark_0.payloads.concatMapValueErrorThrown.evaluate();
            this._Benchmark_benchmark_0.payloads.concatMapValueNull.evaluate();
        }

   }

}