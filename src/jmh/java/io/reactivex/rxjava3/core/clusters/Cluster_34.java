package io.reactivex.rxjava3.core.clusters;

public class Cluster_34 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapSingleTest._Benchmark _Benchmark_benchmark_0;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_34() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
            this._Benchmark_benchmark_0.payloads.mainBoundaryErrorInnerSuccess.evaluate();
            this._Benchmark_benchmark_0.payloads.cancelNoConcurrentClean.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperCrash.evaluate();
        }

   }

}