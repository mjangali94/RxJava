package io.reactivex.rxjava3.core.clusters;

public class Cluster_490 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_490() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
            this._Benchmark_benchmark_0.payloads.basicAsyncFused.evaluate();
            this._Benchmark_benchmark_2.payloads.concatPublisherDelayErrorPrefetch.evaluate();
        }

   }

}