package io.reactivex.rxjava3.core.clusters;

public class Cluster_549 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_549() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.disposeNoNeedForResetTimeBound.evaluate();
            this._Benchmark_benchmark_0.payloads.noHeadRetentionCompleteTime.evaluate();
            this._Benchmark_benchmark_2.payloads.noHeadRetentionCompleteTime.evaluate();
            this._Benchmark_benchmark_2.payloads.disposeNoNeedForResetTimeAndSIzeBound.evaluate();
            this._Benchmark_benchmark_0.payloads.noHeadRetentionErrorTime.evaluate();
            this._Benchmark_benchmark_2.payloads.replaySelectorTime.evaluate();
            this._Benchmark_benchmark_2.payloads.noHeadRetentionErrorTime.evaluate();
            this._Benchmark_benchmark_2.payloads.disposeNoNeedForResetTimeBound.evaluate();
        }

   }

}