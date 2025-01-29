package io.reactivex.rxjava3.core.clusters;

public class Cluster_150 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark _Benchmark_benchmark_0;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_150() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mapperChashConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperNull.evaluate();
        }

   }

}