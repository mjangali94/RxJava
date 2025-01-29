package io.reactivex.rxjava3.core.clusters;

public class Cluster_567 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_0;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_567() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mergeArray.evaluate();
            this._Benchmark_benchmark_0.payloads.merge2.evaluate();
            this._Benchmark_benchmark_0.payloads.merge3.evaluate();
            this._Benchmark_benchmark_0.payloads.merge4.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeArrayFused.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeErrorSuccess.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeSuccessError.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeArrayBackpressured.evaluate();
            this._Benchmark_benchmark_0.payloads.ambArray1SignalsSuccess.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeArrayBackpressuredMixed1.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeArrayBackpressuredMixed2.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeArrayBackpressuredMixed3.evaluate();
            this._Benchmark_benchmark_0.payloads.ambWith1SignalsSuccess.evaluate();
            this._Benchmark_benchmark_0.payloads.ambWith2SignalsSuccess.evaluate();
        }

   }

}