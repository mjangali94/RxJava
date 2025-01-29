package io.reactivex.rxjava3.core.clusters;

public class Cluster_33 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleConcatDelayErrorTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapSingleTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatDelayErrorTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapSingleTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_33() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.normalIterable);
            this._Benchmark_benchmark_1.payloads.innerError.evaluate();
            this._Benchmark_benchmark_1.payloads.limit.evaluate();
            this._Benchmark_benchmark_3.payloads.noSubsequentSubscription.evaluate();
            this._Benchmark_benchmark_1.payloads.basicFusionRejected.evaluate();
            this._Benchmark_benchmark_1.payloads.basicNonFused.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedPollCrash.evaluate();
            this._Benchmark_benchmark_1.payloads.basicAsyncFused.evaluate();
            this._Benchmark_benchmark_3.payloads.noSubsequentSubscriptionIterable.evaluate();
        }

   }

}