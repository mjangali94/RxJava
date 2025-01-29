package io.reactivex.rxjava3.core.clusters;

public class Cluster_32 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatArrayDelayErrorTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatDelayErrorTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapSingleTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatArrayDelayErrorTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatDelayErrorTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_32() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.concat2.evaluate();
            this._Benchmark_benchmark_0.payloads.concatWith.evaluate();
            this._Benchmark_benchmark_0.payloads.concat3.evaluate();
            this._Benchmark_benchmark_0.payloads.concat4.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.normal);
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.normalPublisherPrefetch);
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.normalPublisher);
            this._Benchmark_benchmark_7.payloads.simple.evaluate();
            this._Benchmark_benchmark_7.payloads.basicSyncFused.evaluate();
            this._Benchmark_benchmark_7.payloads.cancel.evaluate();
        }

   }

}