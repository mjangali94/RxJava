package io.reactivex.rxjava3.core.clusters;

public class Cluster_1137 {

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
        public void benchmark_Cluster_1137() throws java.lang.Throwable {
            
            this._Benchmark_benchmark_0.payloads.zip2.evaluate();
            this._Benchmark_benchmark_0.payloads.zip3.evaluate();
            this._Benchmark_benchmark_0.payloads.zipWith.evaluate();
            this._Benchmark_benchmark_0.payloads.zip4.evaluate();
            this._Benchmark_benchmark_0.payloads.zip5.evaluate();
            this._Benchmark_benchmark_0.payloads.zip6.evaluate();
            this._Benchmark_benchmark_0.payloads.zip7.evaluate();
            this._Benchmark_benchmark_0.payloads.zip8.evaluate();
            this._Benchmark_benchmark_0.payloads.zip9.evaluate();
            this._Benchmark_benchmark_0.payloads.ignoreElementErrorMaybe.evaluate();
            this._Benchmark_benchmark_0.payloads.liftThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.using.evaluate();
            this._Benchmark_benchmark_0.payloads.usingNonEager.evaluate();
            this._Benchmark_benchmark_0.payloads.ambIterableOneIsNull.evaluate();
        }

   }

}