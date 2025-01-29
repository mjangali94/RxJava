package io.reactivex.rxjava3.core.clusters;

public class Cluster_983 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleUsingTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapBiSelectorTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleAmbTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleOnErrorCompleteTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleUsingTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapBiSelectorTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleAmbTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleOnErrorCompleteTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_983() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.resourceDisposedIfMapperCrashesNonEager.evaluate();
            this._Benchmark_benchmark_0.payloads.resourceDisposedIfMapperCrashes.evaluate();
            this._Benchmark_benchmark_2.payloads.errorWithJust.evaluate();
            this._Benchmark_benchmark_3.payloads.ambArrayEmpty.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.onSubscribe);
        }

   }

}