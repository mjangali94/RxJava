package io.reactivex.rxjava3.core.clusters;

public class Cluster_1169 {

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
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1169() throws java.lang.Throwable {
            
            this._Benchmark_benchmark_0.payloads.fromCallableThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.wrapCustom.evaluate();
            this._Benchmark_benchmark_0.payloads.unsafeCreate.evaluate();
            this._Benchmark_benchmark_0.payloads.fromRunnable.evaluate();
            this._Benchmark_benchmark_0.payloads.fromAction.evaluate();
            this._Benchmark_benchmark_0.payloads.fromRunnableThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.fromActionThrows.evaluate();
        }

   }

}