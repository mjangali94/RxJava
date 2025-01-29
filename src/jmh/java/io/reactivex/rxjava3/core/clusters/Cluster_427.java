package io.reactivex.rxjava3.core.clusters;

public class Cluster_427 {

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
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_427() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mapReturnNull.evaluate();
            this._Benchmark_benchmark_0.payloads.cast.evaluate();
            this._Benchmark_benchmark_0.payloads.doOnEventSuccessThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.onErrorResumeWithError.evaluate();
            this._Benchmark_benchmark_0.payloads.doOnSubscribeThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.doOnSuccess.evaluate();
            this._Benchmark_benchmark_0.payloads.doOnSubscribe.evaluate();
            this._Benchmark_benchmark_0.payloads.doOnSuccessThrows.evaluate();
        }

   }

}