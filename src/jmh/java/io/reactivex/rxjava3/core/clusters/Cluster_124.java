package io.reactivex.rxjava3.core.clusters;

public class Cluster_124 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_124() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.singleError.evaluate();
            this._Benchmark_benchmark_0.payloads.lastError.evaluate();
            this._Benchmark_benchmark_2.payloads.lastError.evaluate();
        }

   }

}