package io.reactivex.rxjava3.core.clusters;

public class Cluster_474 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapBiSelectorTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleZipIterableTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapBiSelectorTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleZipIterableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_474() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.resultSelectorThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperReturnsNull.evaluate();
            this._Benchmark_benchmark_2.payloads.bothSucceed.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.justWithError.evaluate();
            this._Benchmark_benchmark_5.payloads.singleSourcesInIterable.evaluate();
            this._Benchmark_benchmark_6.payloads.onErrorResumeWith.evaluate();
        }

   }

}