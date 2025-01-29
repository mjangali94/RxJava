package io.reactivex.rxjava3.core.clusters;

public class Cluster_224 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_224() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fusedIsEmptyWithEmptySource.evaluate();
            this._Benchmark_benchmark_0.payloads.flatMapIterablePrefetch.evaluate();
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.concatMapIterableBufferSize);
            this._Benchmark_benchmark_0.payloads.smallPrefetch.evaluate();
            this._Benchmark_benchmark_0.payloads.fusionMethods.evaluate();
            this._Benchmark_benchmark_0.payloads.normalViaFlatMap.evaluate();
            this._Benchmark_benchmark_0.payloads.normalPrefetchViaFlatMap.evaluate();
        }

   }

}