package io.reactivex.rxjava3.core.clusters;

public class Cluster_1008 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleMergeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapSingleTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleMergeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1008() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mergeDelayError3.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeDelayError4.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeDelayErrorPublisher.evaluate();
            this._Benchmark_benchmark_3.payloads.successShortcut.evaluate();
        }

   }

}