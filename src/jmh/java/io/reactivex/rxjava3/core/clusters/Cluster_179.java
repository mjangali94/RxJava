package io.reactivex.rxjava3.core.clusters;

public class Cluster_179 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_179() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.asyncFusedNoneConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.asyncFusedAllConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.asyncFusedMixedConditional.evaluate();
            this._Benchmark_benchmark_3.payloads.windowAbandonmentCancelsUpstreamSkip.evaluate();
        }

   }

}