package io.reactivex.rxjava3.core.clusters;

public class Cluster_826 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDetachTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDetachTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_826() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.zipIterableFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.zipIterable2FunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.zipWithIterableCombinerReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.withLatestFromCombinerReturnsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.dispose.evaluate();
        }

   }

}