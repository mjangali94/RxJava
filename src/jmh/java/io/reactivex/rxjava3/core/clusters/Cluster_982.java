package io.reactivex.rxjava3.core.clusters;

public class Cluster_982 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleFromSupplierTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapMaybeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapMaybeTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.single.SingleBlockingSubscribeTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleFromSupplierTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.single.SingleBlockingSubscribeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_982() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fromSupplierValue.evaluate();
            this._Benchmark_benchmark_0.payloads.fromSupplierTwice.evaluate();
            this._Benchmark_benchmark_0.payloads.fromSupplierError.evaluate();
            this._Benchmark_benchmark_3.payloads.wrap.evaluate();
            this._Benchmark_benchmark_4.payloads.dispose2.evaluate();
            this._Benchmark_benchmark_0.payloads.fromSupplierNull.evaluate();
            this._Benchmark_benchmark_6.payloads.flatMapMaybeError.evaluate();
            this._Benchmark_benchmark_7.payloads.concatMapMaybeError.evaluate();
            this._Benchmark_benchmark_8.runBenchmark(this._Benchmark_benchmark_8.payloads.observerError);
        }

   }

}