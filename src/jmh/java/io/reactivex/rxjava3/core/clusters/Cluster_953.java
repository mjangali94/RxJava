package io.reactivex.rxjava3.core.clusters;

public class Cluster_953 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.ParallelMapTryOptionalTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.ParallelMapOptionalTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.ParallelMapTryOptionalTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.ParallelMapOptionalTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_953() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mapFailWithSkip.evaluate();
            this._Benchmark_benchmark_0.payloads.mapFailWithRetryLimited.evaluate();
            this._Benchmark_benchmark_0.payloads.mapFailWithRetry.evaluate();
            this._Benchmark_benchmark_0.payloads.mapFailWithStop.evaluate();
            this._Benchmark_benchmark_4.payloads.mapCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.mapFailWithSkipConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.mapFailWithRetryLimitedConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.mapFailWithRetryConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.mapFailWithStopConditional.evaluate();
            this._Benchmark_benchmark_4.payloads.doubleFilter.evaluate();
            this._Benchmark_benchmark_0.payloads.mapFailWithError.evaluate();
        }

   }

}